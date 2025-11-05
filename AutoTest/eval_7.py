import subprocess
import json
import sys
import io

# 设置 UTF-8 编码以支持 emoji 输出
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

def ViewFollowingNoteCheck(userId):
    """
    检查用户是否查看了关注列表中第一个博主的第一条笔记
    任务7: 在"我"界面进入"关注"的博主列表，找到第一个博主的第一条笔记并打开
    """
    # 从设备获取关注列表
    follows_result = subprocess.run(
        ['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/follows.json'],
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

    # 从设备获取浏览历史
    browsing_result = subprocess.run(
        ['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/browsing_history.json'],
        capture_output=True,
        encoding='utf-8',
        errors='replace'
    )

    # 检查命令是否成功执行
    if follows_result.returncode != 0 or not follows_result.stdout:
        print(f"❌ Failed to read follows file")
        print(f"   Reason: ADB command failed (return code: {follows_result.returncode})")
        if follows_result.stderr:
            print(f"   Error: {follows_result.stderr}")
        return False

    if browsing_result.returncode != 0 or not browsing_result.stdout:
        print(f"❌ Failed to read browsing history file")
        print(f"   Reason: ADB command failed (return code: {browsing_result.returncode})")
        if browsing_result.stderr:
            print(f"   Error: {browsing_result.stderr}")
        return False

    # 解析 JSON
    try:
        follows_data = json.loads(follows_result.stdout.strip()) if follows_result.stdout.strip() else []
        browsing_data = json.loads(browsing_result.stdout.strip()) if browsing_result.stdout.strip() else []

        # 从浏览历史中提取笔记信息
        notes_data = []
        seen_note_ids = set()
        for item in browsing_data:
            note_id = item.get('noteId')
            author = item.get('noteAuthor', {})
            if note_id and note_id not in seen_note_ids:
                notes_data.append({
                    'id': note_id,
                    'title': item.get('noteTitle', ''),
                    'author': author,
                    'createdAt': item.get('browsedAt', '')  # 使用浏览时间作为近似
                })
                seen_note_ids.add(note_id)
    except (json.JSONDecodeError, TypeError) as e:
        print(f"❌ Failed to parse JSON data")
        print(f"   Reason: Invalid JSON format")
        print(f"   Error: {e}")
        return False

    # 检查是否查看了关注博主的笔记
    try:
        # 检查关注列表是否为空
        if not follows_data or len(follows_data) == 0:
            print(f"❌ Follows list is empty")
            print(f"   Reason: No follow records found")
            print(f"   Expected: At least one following relationship")
            return False

        # 获取用户的关注列表，按关注时间排序
        user_follows = [f for f in follows_data if f.get('followerId') == userId]
        if not user_follows:
            print(f"❌ No following records for user")
            print(f"   Reason: User '{userId}' is not following anyone")
            print(f"   Expected: At least one following relationship")
            return False

        user_follows.sort(key=lambda x: x.get('followedAt', ''))
        first_following_id = user_follows[0].get('followingId')
        first_following_username = user_follows[0].get('following', {}).get('username', 'Unknown')

        # 获取该博主的第一条笔记
        author_notes = [note for note in notes_data
                       if note.get('author', {}).get('id') == first_following_id]
        if not author_notes:
            print(f"❌ First following user has no notes")
            print(f"   Reason: User '{first_following_username}' (id: {first_following_id}) has not published any notes")
            print(f"   Expected: At least one note from this user")
            return False

        author_notes.sort(key=lambda x: x.get('createdAt', ''))
        first_note_id = author_notes[0].get('id')
        first_note_title = author_notes[0].get('title', 'Untitled')

        # 检查浏览历史中是否有查看该笔记的记录，且来源为用户主页
        has_viewed = any(item.get('userId') == userId
                        and item.get('noteId') == first_note_id
                        and item.get('sourceType') == 'USER_PROFILE'
                        for item in browsing_data)

        if has_viewed:
            print(f"✓ Successfully viewed first following user's first note")
            print(f"   Following user: {first_following_username}")
            print(f"   Note: {first_note_title} (id: {first_note_id})")
            return True
        else:
            print(f"❌ Did not view first following user's first note from user profile")
            print(f"   Reason: No browsing record found")
            print(f"   Expected note: {first_note_title} (id: {first_note_id})")
            print(f"   Expected source: USER_PROFILE")
            # Check if viewed from other sources
            other_views = [item for item in browsing_data
                          if item.get('userId') == userId and item.get('noteId') == first_note_id]
            if other_views:
                sources = [item.get('sourceType', 'UNKNOWN') for item in other_views]
                print(f"   Note was viewed from other sources: {sources}")
            return False

    except Exception as e:
        print(f"❌ Error while checking following note view")
        print(f"   Reason: {e}")
        return False

if __name__ == "__main__":
    print(ViewFollowingNoteCheck(
        userId='user_current'
    ))
