# ImageGeneratorForAppSim - 基于豆包大模型的图片生成工具
这是一个简单的 Python 项目，利用火山引擎（Volcengine）的 Ark Runtime SDK 调用豆包大模型（Doubao-Seedream）来生成图片。该项目支持生成单张图片以及根据主题列表批量生成图片，并自动保存到本地。

## 功能特点
- **单张生成**:通过文本提示词生成高质量图片。
- **批量生成**: 支持定义统一背景/风格和多个主题，自动批量生成一系列图片。
- **自动保存**: 生成的图片会自动下载并保存到本地 `generated_images` 目录。

## 环境要求
- Python 3.x

## 安装依赖
请运行以下命令安装所需的 Python 库：
```bash
pip install 'volcengine-python-sdk[ark]' requests
```

## 配置说明
项目默认使用火山引擎的 API。在使用前，请检查 `image_generate.py` 文件中的 `client` 初始化部分，确保填写了有效的 API Key。

```python
# image_generate.py
client = Ark( 
    base_url="https://ark.cn-beijing.volces.com/api/v3", 
    api_key='YOUR_API_KEY_HERE' # 请替换为你自己的 API Key
) 
```

## 使用指南
### 1. 单张图片生成
直接运行 `image_generate.py` 文件。默认示例是生成一张关于“长城”的微信头像。

```bash
python image_generate.py
```

你可以在 `image_generate.py` 的 `if __name__ == "__main__":` 代码块中修改 `prompt` 和 `filename` 来生成你想要的内容。

### 2. 批量图片生成
运行 `batch_generate.py` 文件。该脚本会根据预设的背景描述和主题列表，批量生成多张图片。

```bash
python batch_generate.py
```

你可以在 `batch_generate.py` 中修改以下参数来自定义批量生成任务：
- `background`: 图片的通用描述或风格要求（例如：“生成一些菜品图，尽可能真实...”）。
- `themes`: 一个包含具体主题的列表（例如：`["火锅", "烤肉", "烧烤"]`）。

## 项目结构
- `image_generate.py`: 核心功能文件。包含 `generate_and_save_image` 函数，负责调用 API 生成图片并处理下载保存逻辑。
- `batch_generate.py`: 批量处理脚本。引用 `image_generate.py` 中的功能，遍历主题列表进行批量生成。