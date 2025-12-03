import os 
import requests
import uuid
from datetime import datetime
from volcenginesdkarkruntime import Ark


client = Ark( 
    base_url="https://ark.cn-beijing.volces.com/api/v3", 
    api_key='dbe76e1c-a494-4775-9ab5-32e545a5ac3a'
) 

def download_image(url, save_path):
    """
    从URL下载图片并保存到本地
    
    Args:
        url (str): 图片的URL地址
        save_path (str): 保存路径
    
    Returns:
        bool: 下载是否成功
    """
    try:
        # 发送GET请求下载图片
        response = requests.get(url, timeout=30)
        response.raise_for_status()  # 检查请求是否成功
        
        # 确保保存目录存在
        os.makedirs(os.path.dirname(save_path), exist_ok=True)
        
        # 写入文件
        with open(save_path, 'wb') as f:
            f.write(response.content)
        
        print(f"图片已成功保存到: {save_path}")
        return True
        
    except requests.exceptions.RequestException as e:
        print(f"下载图片失败: {e}")
        return False
    except Exception as e:
        print(f"保存图片失败: {e}")
        return False

def generate_and_save_image(prompt, filename=None, save_dir="generated_images"):
    """
    生成图片并保存到本地
    
    Args:
        prompt (str): 图片生成提示词
        filename (str, optional): 保存的文件名，默认使用时间戳
        save_dir (str): 保存目录
    
    Returns:
        str: 保存的文件路径，如果失败返回None
    """
    try:
        # 生成图片
        print(f"正在生成图片: {prompt}")
        imagesResponse = client.images.generate( 
            model="doubao-seedream-4-0-250828", 
            prompt=prompt,
            size="2K",
            response_format="url",
            watermark=True
        ) 
        
        # 获取生成的图片URL
        image_url = imagesResponse.data[0].url
        print(f"图片生成成功，URL: {image_url}")
        
        # 生成文件名
        if filename is None:
            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            unique_id = str(uuid.uuid4())[:8]
            filename = f"generated_{timestamp}_{unique_id}.jpg"
        
        # 确保文件扩展名
        if not filename.lower().endswith(('.jpg', '.jpeg', '.png', '.webp')):
            filename += '.jpg'
        
        # 构建保存路径
        save_path = os.path.join(save_dir, filename)
        
        # 下载并保存图片
        if download_image(image_url, save_path):
            print(f"图片已成功保存到: {save_path}")
            return save_path
        else:
            return None
            
    except Exception as e:
        print(f"生成图片失败: {e}")
        return None

if __name__ == "__main__":
    avatar_path = generate_and_save_image(
        prompt="生成一个微信头像, 内容是长城",
        filename="wechat_avatar_greatwall.jpg"
    )
    print("图片保存路径: ", avatar_path)