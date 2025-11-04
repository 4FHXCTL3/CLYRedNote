import subprocess
import json

def FollowAuthorCheck(userId, authorUsername):
    """
    检查用户是否关注了指定的博主
    任务5: 在首页找到博主"小红薯美妆达人"的笔记，进入笔记并点击"关注"
    """
    # 从设备获取关注列表
    result = subprocess.run(
        ['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/follows.json'],
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

    # 检查关注列表
    try:
        if not data or len(data) == 0:
            return False

        # 查找用户是否关注了指定博主
        for follow in data:
            if (follow.get('followerId') == userId and
                follow.get('following', {}).get('username') == authorUsername):
                return True

        return False

    except Exception as e:
        print(f"Error: {e}")
        return False

if __name__ == "__main__":
    print(FollowAuthorCheck(
        userId='user_current',
        authorUsername='cly_beauty'  # 小红薯美妆达人的username
    ))
