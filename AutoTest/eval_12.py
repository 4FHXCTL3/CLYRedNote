import subprocess
import json

def PublishNoteCheck(userId, noteTitle='今日份分享', noteContent='天晴了'):
    """
    检查用户是否发布了指定标题和内容的笔记
    任务12: 点击底部栏的"+"号，点击添加图片，并输入文字"天晴了"，
           进入下一步，添加标题为"今日份分享"，笔记设为"仅自己可见"，最后发布笔记
    """
    # 从设备获取笔记列表
    result = subprocess.run(
        ['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/notes.json'],
        capture_output=True,
        encoding='utf-8',
        errors='replace'
    )

    # 检查命令是否成功执行
    if result.returncode != 0 or not result.stdout:
        print(f"❌ Failed to read notes file")
        print(f"   Reason: ADB command failed (return code: {result.returncode})")
        if result.stderr:
            print(f"   Error: {result.stderr}")
        return False

    # 解析 JSON
    try:
        data = json.loads(result.stdout)
    except (json.JSONDecodeError, TypeError) as e:
        print(f"❌ Failed to parse notes data")
        print(f"   Reason: Invalid JSON format")
        print(f"   Error: {e}")
        return False

    # 检查笔记发布
    try:
        if not data or len(data) == 0:
            print(f"❌ Notes list is empty")
            print(f"   Reason: No notes found in system")
            print(f"   Expected: At least one note published by user '{userId}'")
            return False

        # 查找符合条件的笔记
        for note in data:
            if (note.get('author', {}).get('id') == userId and
                note.get('title') == noteTitle and
                noteContent in note.get('content', '') and
                note.get('visibility') == 'PRIVATE'):  # 仅自己可见
                print(f"✓ Successfully published note")
                print(f"   Title: {noteTitle}")
                print(f"   Content: {noteContent}")
                print(f"   Visibility: PRIVATE (仅自己可见)")
                print(f"   Note ID: {note.get('id', 'Unknown')}")
                return True

        # Check if note exists with wrong attributes
        user_notes = [note for note in data if note.get('author', {}).get('id') == userId]
        if not user_notes:
            print(f"❌ No notes found for user")
            print(f"   Reason: User '{userId}' has not published any notes")
            print(f"   Expected: Note with title '{noteTitle}' and content '{noteContent}'")
            return False

        # Check for partial matches
        title_match = [n for n in user_notes if n.get('title') == noteTitle]
        content_match = [n for n in user_notes if noteContent in n.get('content', '')]
        visibility_match = [n for n in user_notes if n.get('visibility') == 'PRIVATE']

        print(f"❌ Note not found with all required attributes")
        print(f"   Reason: Could not find note matching all criteria")
        print(f"   Expected title: '{noteTitle}' (found {len(title_match)} matches)")
        print(f"   Expected content containing: '{noteContent}' (found {len(content_match)} matches)")
        print(f"   Expected visibility: PRIVATE (found {len(visibility_match)} matches)")
        print(f"   Total notes by user: {len(user_notes)}")

        # Show recent notes by user
        if user_notes:
            recent_note = user_notes[-1]
            print(f"   Most recent note: title='{recent_note.get('title', 'N/A')}', " +
                  f"visibility={recent_note.get('visibility', 'N/A')}")

        return False

    except Exception as e:
        print(f"❌ Error while checking published note")
        print(f"   Reason: {e}")
        return False

if __name__ == "__main__":
    print(PublishNoteCheck(
        userId='user_current',
        noteTitle='今日份分享',
        noteContent='天晴了'
    ))
