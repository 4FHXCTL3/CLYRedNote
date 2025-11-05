import subprocess
import json

def ViewSecondCollectionCheck(userId, position=2):
    """
    检查用户是否查看了收藏中的第N个笔记并进行点赞、收藏
    任务19: 进入你的个人主页，查看"收藏"，浏览第2个笔记的内容并点赞、收藏
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

    # 从设备获取点赞记录
    likes_result = subprocess.run(
        ['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/likes.json'],
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
        return False
    if likes_result.returncode != 0:
        print(f"❌ Failed to read likes file")
        print(f"   Reason: ADB command failed (return code: {likes_result.returncode})")
        return False

    # 解析 JSON
    try:
        collections_data = json.loads(collections_result.stdout)
        browsing_data = json.loads(browsing_result.stdout)
        likes_data = json.loads(likes_result.stdout) if likes_result.stdout else []
    except (json.JSONDecodeError, TypeError) as e:
        print(f"❌ Failed to parse JSON data")
        print(f"   Reason: Invalid JSON format")
        print(f"   Error: {e}")
        return False

    # 检查收藏浏览和互动
    try:
        # 获取用户的收藏列表
        user_collections = [col for col in collections_data
                           if col.get('userId') == userId]

        if not user_collections:
            print(f"❌ Collections list is empty")
            print(f"   Reason: User has no collections")
            return False

        if len(user_collections) < position:
            print(f"❌ Not enough collections")
            print(f"   Reason: User has {len(user_collections)} collection(s), expected at least {position}")
            return False

        # 获取第N个收藏的笔记
        sorted_collections = sorted(user_collections, key=lambda x: x.get('collectedAt', ''), reverse=True)
        target_collection = sorted_collections[position - 1]
        note_id = target_collection.get('noteId')
        collection_time = target_collection.get('collectedAt')

        # 检查是否从收藏页面浏览了这个笔记
        browsed_from_collection = any(item.get('userId') == userId
                                     and item.get('noteId') == note_id
                                     and item.get('sourceType') == 'COLLECTION'
                                     and item.get('browsedAt', '') >= collection_time
                                     for item in browsing_data)

        # 检查是否点赞
        has_liked = any(like.get('userId') == userId
                       and like.get('targetId') == note_id
                       and like.get('targetType') == 'NOTE'
                       for like in likes_data)

        # 检查是否收藏（应该已经收藏，检查是否重复收藏）
        collection_count = sum(1 for col in collections_data
                              if col.get('userId') == userId
                              and col.get('noteId') == note_id)

        if browsed_from_collection and has_liked and collection_count >= 1:
            print(f"✓ Successfully viewed collection #{position} and interacted")
            print(f"   Note ID: {note_id}")
            print(f"   Browsed from collection: ✓")
            print(f"   Liked: ✓")
            print(f"   Collection count: {collection_count}")
            return True
        else:
            print(f"❌ Missing interactions on collection #{position}")
            print(f"   Note ID: {note_id}")
            print(f"   Browsed from collection: {'✓' if browsed_from_collection else '✗'}")
            print(f"   Liked: {'✓' if has_liked else '✗'}")
            print(f"   Collection count: {collection_count}")
            # Show if viewed from other sources
            other_views = [item for item in browsing_data
                          if item.get('userId') == userId
                          and item.get('noteId') == note_id]
            if other_views:
                sources = [item.get('sourceType', 'UNKNOWN') for item in other_views]
                print(f"   Viewed from other sources: {sources}")
            return False

    except Exception as e:
        print(f"❌ Error while checking collection view")
        print(f"   Reason: {e}")
        return False

if __name__ == "__main__":
    print(ViewSecondCollectionCheck(
        userId='user_current',
        position=2
    ))
