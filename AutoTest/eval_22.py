import subprocess
import json

def CountAuthorNotesCheck(authorNickname="旅行日记", minCount=0):
    """
    检查并统计指定博主发布的笔记数量
    任务22: 统计博主"旅行日记"发布笔记数量
    """
    # 从设备获取笔记列表
    notes_result = subprocess.run(
        ['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/notes.json'],
        capture_output=True,
        encoding='utf-8',
        errors='replace'
    )

    # 从设备获取用户列表
    users_result = subprocess.run(
        ['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/users.json'],
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
    if users_result.returncode != 0 or not users_result.stdout:
        print(f"❌ Failed to read users file")
        print(f"   Reason: ADB command failed (return code: {users_result.returncode})")
        return False

    # 解析 JSON
    try:
        notes_data = json.loads(notes_result.stdout)
        users_data = json.loads(users_result.stdout)
    except (json.JSONDecodeError, TypeError) as e:
        print(f"❌ Failed to parse JSON data")
        print(f"   Reason: Invalid JSON format")
        print(f"   Error: {e}")
        return False

    # 统计博主笔记数量
    try:
        # 查找指定昵称的博主
        target_author = None
        for user in users_data:
            if user.get('nickname') == authorNickname:
                target_author = user
                break

        if not target_author:
            print(f"❌ Author not found")
            print(f"   Reason: No user with nickname '{authorNickname}'")
            # Show available nicknames
            available_nicknames = [u.get('nickname', 'UNKNOWN') for u in users_data][:10]
            print(f"   Available nicknames: {available_nicknames}")
            return False

        author_id = target_author.get('id')

        # 统计该博主的笔记数量
        author_notes = [note for note in notes_data
                       if note.get('author', {}).get('id') == author_id]

        note_count = len(author_notes)

        if note_count >= minCount:
            print(f"✓ Successfully counted author notes")
            print(f"   Author: '{authorNickname}' (ID: {author_id})")
            print(f"   Total notes: {note_count}")
            return True
        else:
            print(f"❌ Not enough notes by author")
            print(f"   Author: '{authorNickname}' (ID: {author_id})")
            print(f"   Found {note_count} notes, expected at least {minCount}")
            return False

    except Exception as e:
        print(f"❌ Error while counting author notes")
        print(f"   Reason: {e}")
        return False

if __name__ == "__main__":
    print(CountAuthorNotesCheck(
        authorNickname='旅行日记',
        minCount=0
    ))
