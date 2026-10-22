"""生成模板用的本地占位图（不依赖外网图床）。

为什么需要这个脚本：这些图是二进制文件，没有生成脚本就等于不可维护——
想换配色只能重新找图。改下面的 BANNER_PALETTES 后重跑本脚本即可。

    python scripts/gen-placeholder-images.py

依赖：Pillow（pip install pillow）
产出：app/src/main/res/drawable-nodpi/ 下的
      banner_1..6.png / avatar_default.png / cover_placeholder.png
尺寸：放在 -nodpi 目录，避免不同密度二次缩放；Banner 2:1 与常见广告位比例一致。
风格：对角线性渐变 + 半透明几何装饰，接近真实广告图，避免"图片坏了"的观感。
"""

import os

from PIL import Image, ImageDraw, ImageFilter

# 输出到 app 模块的 res 目录（脚本位于 scripts/，仓库根是上一级）
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "app", "src", "main", "res", "drawable-nodpi")
os.makedirs(OUT, exist_ok=True)

BANNER_PALETTES = [
    ((30, 58, 138), (14, 165, 233)),    # 深蓝 → 天青
    ((109, 40, 217), (236, 72, 153)),   # 紫 → 粉
    ((234, 88, 12), (220, 38, 38)),     # 橙 → 红
    ((4, 120, 87), (16, 185, 129)),     # 墨绿 → 翠绿
    ((67, 56, 202), (59, 130, 246)),    # 靛 → 蓝
    ((190, 18, 60), (245, 158, 11)),    # 玫瑰红 → 琥珀
]


def diagonal_gradient(size, start, end):
    """135° 对角渐变。逐像素算 t，再按 t 插值。"""
    w, h = size
    img = Image.new("RGB", size)
    px = img.load()
    # 投影到对角线方向，归一化到 0..1
    for y in range(h):
        for x in range(w):
            t = (x / (w - 1) + y / (h - 1)) / 2.0
            px[x, y] = (
                round(start[0] + (end[0] - start[0]) * t),
                round(start[1] + (end[1] - start[1]) * t),
                round(start[2] + (end[2] - start[2]) * t),
            )
    return img


def add_decoration(img, seed):
    """叠加半透明圆形与斜向高光，制造层次感。"""
    w, h = img.size
    overlay = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    draw = ImageDraw.Draw(overlay)

    # 大圆：右上角外溢
    r1 = int(w * 0.42)
    cx1, cy1 = int(w * 0.82), int(-h * 0.12)
    draw.ellipse([cx1 - r1, cy1 - r1, cx1 + r1, cy1 + r1], fill=(255, 255, 255, 26))

    # 小圆：左下角
    r2 = int(w * 0.22)
    cx2, cy2 = int(w * 0.12), int(h * 1.05)
    draw.ellipse([cx2 - r2, cy2 - r2, cx2 + r2, cy2 + r2], fill=(255, 255, 255, 20))

    # 斜向高光带
    band_x = w * (0.30 + 0.08 * (seed % 3))
    draw.polygon(
        [
            (band_x, h),
            (band_x + w * 0.10, h),
            (band_x + w * 0.28, 0),
            (band_x + w * 0.18, 0),
        ],
        fill=(255, 255, 255, 16),
    )

    overlay = overlay.filter(ImageFilter.GaussianBlur(radius=max(2, w // 200)))
    return Image.alpha_composite(img.convert("RGBA"), overlay).convert("RGB")


def make_banner(index, palette, size=(1080, 540)):
    img = diagonal_gradient(size, palette[0], palette[1])
    img = add_decoration(img, index)
    path = os.path.join(OUT, f"banner_{index + 1}.png")
    img.save(path, "PNG", optimize=True)
    return path


def make_avatar(size=240):
    """圆形头像底图：径向渐变感 + 人形剪影，避免纯色块。"""
    img = diagonal_gradient((size, size), (100, 116, 139), (71, 85, 105))
    overlay = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(overlay)
    # 头
    head_r = int(size * 0.17)
    hx, hy = size // 2, int(size * 0.38)
    draw.ellipse([hx - head_r, hy - head_r, hx + head_r, hy + head_r], fill=(255, 255, 255, 205))
    # 肩
    body_w, body_h = int(size * 0.52), int(size * 0.40)
    draw.ellipse(
        [hx - body_w // 2, hy + head_r, hx + body_w // 2, hy + head_r + body_h],
        fill=(255, 255, 255, 205),
    )
    img = Image.alpha_composite(img.convert("RGBA"), overlay).convert("RGB")
    path = os.path.join(OUT, "avatar_default.png")
    img.save(path, "PNG", optimize=True)
    return path


def make_cover(size=(600, 450)):
    """文章封面占位：中性灰蓝渐变 + 山形轮廓。"""
    img = diagonal_gradient(size, (148, 163, 184), (100, 116, 139))
    w, h = size
    overlay = Image.new("RGBA", size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(overlay)
    # 远山
    draw.polygon([(0, h), (w * 0.34, h * 0.42), (w * 0.62, h), ], fill=(255, 255, 255, 40))
    draw.polygon([(w * 0.40, h), (w * 0.74, h * 0.30), (w, h * 0.72), (w, h)], fill=(255, 255, 255, 28))
    img = Image.alpha_composite(img.convert("RGBA"), overlay).convert("RGB")
    path = os.path.join(OUT, "cover_placeholder.png")
    img.save(path, "PNG", optimize=True)
    return path


if __name__ == "__main__":
    paths = [make_banner(i, p) for i, p in enumerate(BANNER_PALETTES)]
    paths.append(make_avatar())
    paths.append(make_cover())
    total = 0
    for p in paths:
        size_kb = os.path.getsize(p) / 1024
        total += size_kb
        print(f"{os.path.basename(p):24s} {size_kb:7.1f} KB")
    print(f"{'合计':24s} {total:7.1f} KB")
