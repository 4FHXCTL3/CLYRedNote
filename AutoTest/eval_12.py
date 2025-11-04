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
        return False

    # 解析 JSON
    try:
        data = json.loads(result.stdout)
    except (json.JSONDecodeError, TypeError):
        return False

    # 检查笔记发布
    try:
        if not data or len(data) == 0:
            return False

        # 查找符合条件的笔记
        for note in data:
            if (note.get('author', {}).get('id') == userId and
                note.get('title') == noteTitle and
                noteContent in note.get('content', '') and
                note.get('visibility') == 'PRIVATE'):  # 仅自己可见
                return True

        return False

    except Exception as e:
        print(f"Error: {e}")
        return False

if __name__ == "__main__":
    print(PublishNoteCheck(
        userId='user_current',
        noteTitle='今日份分享',
        noteContent='天晴了'
    ))
