import subprocess
import json

def PublishAndSelfInteractCheck(userId, noteTitle="今日分享", noteContent="今天也要加油呀", visibility="PUBLIC"):
    """
    检查用户是否发布了指定笔记并对其进行点赞和收藏
    任务18: 点击底部栏的"+"号，点击添加图片，并输入文字"今天也要加油呀"，
           进入下一步，添加标题为"今日分享"，笔记设为"公开可见"，最后发布笔记并对这篇笔记进行点赞、收藏
    """
    # 从设备获取笔记列表
    notes_result = subprocess.run(
        ['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/notes.json'],
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

    # 从设备获取收藏记录
    collections_result = subprocess.run(
        ['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/collections.json'],
        capture_output=True,
        encoding='utf-8',
        errors='replace'
    )

    # 检查命令是否成功执行
    if notes_result.returncode != 0 or not notes_result.stdout:
        print(f"❌ Failed to read notes file")
        print(f"   Reason: ADB command failed (return code: {notes_result.returncode})")
        if notes_result.stderr:
            print(f"   Error: {notes_result.stderr}")
        return False
    if likes_result.returncode != 0:
        print(f"❌ Failed to read likes file")
        print(f"   Reason: ADB command failed (return code: {likes_result.returncode})")
        return False
    if collections_result.returncode != 0:
        print(f"❌ Failed to read collections file")
        print(f"   Reason: ADB command failed (return code: {collections_result.returncode})")
        return False

    # 解析 JSON
    try:
        notes_data = json.loads(notes_result.stdout)
        likes_data = json.loads(likes_result.stdout) if likes_result.stdout else []
        collections_data = json.loads(collections_result.stdout) if collections_result.stdout else []
    except (json.JSONDecodeError, TypeError) as e:
        print(f"❌ Failed to parse JSON data")
        print(f"   Reason: Invalid JSON format")
        print(f"   Error: {e}")
        return False

    # 检查笔记发布和互动
    try:
        # 查找用户发布的匹配笔记
        user_notes = [note for note in notes_data
                     if note.get('author', {}).get('id') == userId
                     and note.get('title') == noteTitle
                     and note.get('content') == noteContent
                     and note.get('visibility') == visibility]

        if not user_notes:
            print(f"❌ Published note not found")
            print(f"   Expected: Title='{noteTitle}', Content='{noteContent}', Visibility='{visibility}'")
            # Show recent notes by user
            recent_user_notes = [n for n in notes_data if n.get('author', {}).get('id') == userId]
            if recent_user_notes:
                latest = sorted(recent_user_notes, key=lambda x: x.get('createdAt', ''), reverse=True)[0]
                print(f"   Latest note by user:")
                print(f"     Title: '{latest.get('title', 'N/A')}'")
                print(f"     Content: '{latest.get('content', 'N/A')}'")
                print(f"     Visibility: '{latest.get('visibility', 'N/A')}'")
            return False

        # 获取最新匹配的笔记
        target_note = sorted(user_notes, key=lambda x: x.get('createdAt', ''), reverse=True)[0]
        note_id = target_note.get('id')

        # 检查是否点赞
        has_liked = any(like.get('userId') == userId
                       and like.get('targetId') == note_id
                       and like.get('targetType') == 'NOTE'
                       for like in likes_data)

        # 检查是否收藏
        has_collected = any(col.get('userId') == userId
                           and col.get('noteId') == note_id
                           for col in collections_data)

        if has_liked and has_collected:
            print(f"✓ Successfully published and interacted with note")
            print(f"   Note ID: {note_id}")
            print(f"   Title: '{noteTitle}'")
            print(f"   Liked: ✓, Collected: ✓")
            return True
        else:
            print(f"❌ Note published but missing interactions")
            print(f"   Note ID: {note_id}")
            print(f"   Title: '{noteTitle}'")
            print(f"   Liked: {'✓' if has_liked else '✗'}")
            print(f"   Collected: {'✓' if has_collected else '✗'}")
            return False

    except Exception as e:
        print(f"❌ Error while checking published note")
        print(f"   Reason: {e}")
        return False

if __name__ == "__main__":
    print(PublishAndSelfInteractCheck(
        userId='user_current',
        noteTitle='今日分享',
        noteContent='今天也要加油呀',
        visibility='PUBLIC'
    ))
