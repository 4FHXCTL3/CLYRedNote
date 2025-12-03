from image_generate import generate_and_save_image


def batch_generate_image(background: str, themes: list[str], save_dir="generated_images"):
    """
    批量生成图片
    
    Args:
        background (str): 这批图片的共同背景和生成要求，如: "生成微信图像，采用暖色调。"
        themes (list[str]): 主题列表，如: ["旅游", "美食", "文化", "历史", "艺术", "科技"]
        save_dir (str): 保存目录, 默认为"generated_images"
    """

    for theme in themes:
        prompt = f"要求:{background}, 主题为:{theme}"
        generate_and_save_image(prompt, filename=f"{theme}.jpg", save_dir=save_dir)
    

if __name__ == "__main__":
    batch_generate_image(background="生成一些小红书笔记的封面图，尽可能真实，图片尺寸为1024x1024",
                        themes=["穿搭", "美食", "学习", "旅行", "科普", "家居","职场","漫画","彩妆","摄影"],
                        save_dir="generated_images")