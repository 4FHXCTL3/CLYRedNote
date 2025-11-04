import subprocess
import json

def ChangeNicknameCheck(userId, expectedNickname='111'):
    """
    检查用户是否修改了昵称
    任务11: 在"我"打开"编辑资料"，修改自己的名字为"111"
    """
    # 从设备获取用户数据
    result = subprocess.run(
        ['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/users.json'],
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

    # 检查昵称修改
    try:
        if not data or len(data) == 0:
            return False

        # 查找当前用户
        for user in data:
            if user.get('id') == userId:
                # 检查昵称是否已修改
                nickname = user.get('nickname', '')
                return nickname == expectedNickname

        return False

    except Exception as e:
        print(f"Error: {e}")
        return False

if __name__ == "__main__":
    print(ChangeNicknameCheck(
        userId='user_current',
        expectedNickname='111'
    ))
