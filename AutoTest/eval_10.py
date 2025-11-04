import subprocess
import json

def SetPasswordCheck(userId, expectedPassword='123456'):
    """
    检查用户是否设置了登录密码
    任务10: 在"我"打开编辑资料右侧的设置按钮，找到"账号与安全"选项，设置登录密码为123456
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

    # 检查密码设置
    try:
        if not data or len(data) == 0:
            return False

        # 查找当前用户
        for user in data:
            if user.get('id') == userId:
                # 检查是否设置了密码（简化处理：检查password字段）
                password = user.get('password', '')
                return password == expectedPassword

        return False

    except Exception as e:
        print(f"Error: {e}")
        return False

if __name__ == "__main__":
    print(SetPasswordCheck(
        userId='user_current',
        expectedPassword='123456'
    ))
