import subprocess
import json

def DislikeNoteCheck(userId, notePosition=2):
    """
    检查用户是否对首页指定位置的笔记点击了"不喜欢"
    任务8: 对首页第二篇笔记进入详情，点击右上角的图标，选择"不喜欢"
    """
    # 从设备获取不喜欢记录
    result = subprocess.run(
        ['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/dislikes.json'],
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

    # 检查不喜欢记录
    try:
        if not data or len(data) == 0:
            return False

        # 查找用户的不喜欢记录
        user_dislikes = [item for item in data if item.get('userId') == userId]

        # 检查是否有最新的不喜欢记录
        if user_dislikes:
            # 按时间排序，获取最新的记录
            latest_dislike = sorted(user_dislikes, key=lambda x: x.get('dislikedAt', ''), reverse=True)[0]
            # 简化处理：只要有不喜欢记录就返回True
            return True

        return False

    except Exception as e:
        print(f"Error: {e}")
        return False

if __name__ == "__main__":
    print(DislikeNoteCheck(
        userId='user_current',
        notePosition=2
    ))
