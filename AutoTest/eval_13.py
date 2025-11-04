import subprocess
import json

def ShareNoteCheck(userId):
    """
    检查用户是否分享了首页第一篇笔记
    任务13: 在首页第一篇笔记详情页点击右上角"分享"按钮
    """
    # 从设备获取分享记录
    result = subprocess.run(
        ['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/shares.json'],
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

    # 检查分享记录
    try:
        if not data or len(data) == 0:
            return False

        # 查找用户的分享记录
        user_shares = [item for item in data if item.get('userId') == userId]

        # 如果有最新的分享记录，返回 True
        if user_shares:
            # 按时间排序，获取最新的分享
            latest_share = sorted(user_shares, key=lambda x: x.get('sharedAt', ''), reverse=True)[0]
            return True

        return False

    except Exception as e:
        print(f"Error: {e}")
        return False

if __name__ == "__main__":
    print(ShareNoteCheck(
        userId='user_current'
    ))
