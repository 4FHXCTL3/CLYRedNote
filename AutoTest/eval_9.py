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
    if collections_result.returncode != 0 or not collections_result.stdout:
        print(f"❌ Failed to read collections file")
        print(f"   Reason: ADB command failed (return code: {collections_result.returncode})")
        if collections_result.stderr:
            print(f"   Error: {collections_result.stderr}")
        return False

    if browsing_result.returncode != 0 or not browsing_result.stdout:
        print(f"❌ Failed to read browsing history file")
        print(f"   Reason: ADB command failed (return code: {browsing_result.returncode})")
        if browsing_result.stderr:
            print(f"   Error: {browsing_result.stderr}")
        return False

    # 解析 JSON
    try:
        collections_data = json.loads(collections_result.stdout)
        browsing_data = json.loads(browsing_result.stdout)
    except (json.JSONDecodeError, TypeError) as e:
        print(f"❌ Failed to parse JSON data")
        print(f"   Reason: Invalid JSON format")
        print(f"   Error: {e}")
        return False

    # 检查是否查看了收藏的笔记
    try:
        # 检查收藏列表是否为空
        if not collections_data or len(collections_data) == 0:
            print(f"❌ Collections list is empty")
            print(f"   Reason: No collection records found")
            print(f"   Expected: At least one collection record")
            return False

        # 获取用户的收藏列表，按收藏时间排序
        user_collections = [c for c in collections_data if c.get('userId') == userId]
        if not user_collections:
            print(f"❌ No collections for user")
            print(f"   Reason: User '{userId}' has not collected any notes")
            print(f"   Expected: At least one collection record")
            print(f"   Total collections in system: {len(collections_data)}")
            return False

        user_collections.sort(key=lambda x: x.get('collectedAt', ''))
        first_collection_note_id = user_collections[0].get('noteId')
        first_collection_time = user_collections[0].get('collectedAt', 'Unknown')

        # 检查浏览历史中是否有查看该笔记的记录，且来源为收藏夹
        has_viewed = any(item.get('userId') == userId
                        and item.get('noteId') == first_collection_note_id
                        and item.get('sourceType') == 'COLLECTION'
                        for item in browsing_data)

        if has_viewed:
            print(f"✓ Successfully viewed first collected note from collection")
            print(f"   Note ID: {first_collection_note_id}")
            print(f"   Collected at: {first_collection_time}")
            return True
        else:
            print(f"❌ Did not view first collected note from collection")
            print(f"   Reason: No browsing record found")
            print(f"   Expected note ID: {first_collection_note_id}")
            print(f"   Expected source: COLLECTION")
            # Check if viewed from other sources
            other_views = [item for item in browsing_data
                          if item.get('userId') == userId and item.get('noteId') == first_collection_note_id]
            if other_views:
                sources = [item.get('sourceType', 'UNKNOWN') for item in other_views]
                print(f"   Note was viewed from other sources: {sources}")
            return False

    except Exception as e:
        print(f"❌ Error while checking collection view")
        print(f"   Reason: {e}")
        return False

if __name__ == "__main__":
    print(ViewCollectionCheck(
        userId='user_current'
    ))
