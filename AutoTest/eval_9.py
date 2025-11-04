import subprocess
import json

def ViewCollectionCheck(userId):
    """
    检查用户是否查看了收藏中的第一个笔记
    任务9: 进入你的个人主页，查看"收藏"，并浏览第一个笔记的内容
    """
    # 从设备获取收藏列表
    collections_result = subprocess.run(
        ['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/collections.json'],
        capture_output=True,
        encoding='utf-8',
        errors='replace'
    )

    # 从设备获取浏览历史
    browsing_result = subprocess.run(
        ['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/browsing_history.json'],
        capture_output=True,
        encoding='utf-8',
        errors='replace'
    )

    # 检查命令是否成功执行
    if collections_result.returncode != 0 or browsing_result.returncode != 0:
        return False

    # 解析 JSON
    try:
        collections_data = json.loads(collections_result.stdout) if collections_result.stdout else []
        browsing_data = json.loads(browsing_result.stdout) if browsing_result.stdout else []
    except (json.JSONDecodeError, TypeError):
        return False

    # 检查是否查看了收藏的笔记
    try:
        # 获取用户的收藏列表，按收藏时间排序
        user_collections = [c for c in collections_data if c.get('userId') == userId]
        if not user_collections:
            return False

        user_collections.sort(key=lambda x: x.get('collectedAt', ''))
        first_collection_note_id = user_collections[0].get('noteId')

        # 检查浏览历史中是否有查看该笔记的记录，且来源为收藏夹
        has_viewed = any(item.get('userId') == userId
                        and item.get('noteId') == first_collection_note_id
                        and item.get('sourceType') == 'COLLECTION'
                        for item in browsing_data)

        return has_viewed

    except Exception as e:
        print(f"Error: {e}")
        return False

if __name__ == "__main__":
    print(ViewCollectionCheck(
        userId='user_current'
    ))
