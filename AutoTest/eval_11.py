import subprocess
import json
import sys
import io

# 设置 UTF-8 编码以支持 emoji 输出
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

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
        print(f"❌ Failed to read users file")
        print(f"   Reason: ADB command failed (return code: {result.returncode})")
        if result.stderr:
            print(f"   Error: {result.stderr}")
        return False

    # 解析 JSON
    try:
        data = json.loads(result.stdout)
    except (json.JSONDecodeError, TypeError) as e:
        print(f"❌ Failed to parse users data")
        print(f"   Reason: Invalid JSON format")
        print(f"   Error: {e}")
        return False

    # 检查昵称修改
    try:
        if not data or len(data) == 0:
            print(f"❌ Users list is empty")
            print(f"   Reason: No user records found")
            print(f"   Expected: At least one user record")
            return False

        # 查找当前用户
        for user in data:
            if user.get('id') == userId:
                # 检查昵称是否已修改
                nickname = user.get('nickname', '')
                if nickname == expectedNickname:
                    print(f"✓ Successfully changed nickname")
                    print(f"   User ID: {userId}")
                    print(f"   New nickname: {expectedNickname}")
                    return True
                else:
                    print(f"❌ Nickname does not match expected value")
                    print(f"   Reason: User nickname is '{nickname}', expected '{expectedNickname}'")
                    if not nickname:
                        print(f"   Note: Nickname field is empty or not set")
                    return False

        print(f"❌ User not found")
        print(f"   Reason: User '{userId}' does not exist in users list")
        print(f"   Total users in system: {len(data)}")
        # Show available user IDs
        user_ids = [user.get('id', 'Unknown') for user in data[:5]]
        if user_ids:
            print(f"   Available user IDs (first 5): {user_ids}")
        return False

    except Exception as e:
        print(f"❌ Error while checking nickname")
        print(f"   Reason: {e}")
        return False

if __name__ == "__main__":
    print(ChangeNicknameCheck(
        userId='user_current',
        expectedNickname='111'
    ))
