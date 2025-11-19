import subprocess
import json
import sys
import io

# 设置 UTF-8 编码以支持 emoji 输出
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

def FollowBackFanCheck(userId, fanPosition=1):
    """
    检查用户是否回关了指定位置的粉丝
    任务20: 在"我"->"粉丝"对第一个粉丝进行"回关"
    """
    # 从设备获取关注列表
    follows_result = subprocess.run(
        ['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/follows.json'],
        capture_output=True,
        encoding='utf-8',
        errors='replace'
    )

    # 从设备获取用户列表（包含粉丝信息）
    users_result = subprocess.run(
        ['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/users.json'],
        capture_output=True,
        encoding='utf-8',
        errors='replace'
    )

    # 检查命令是否成功执行
    if follows_result.returncode != 0 or not follows_result.stdout:
        print(f"❌ Failed to read follows file")
        print(f"   Reason: ADB command failed (return code: {follows_result.returncode})")
        if follows_result.stderr:
            print(f"   Error: {follows_result.stderr}")
        return False
    if users_result.returncode != 0 or not users_result.stdout:
        print(f"❌ Failed to read users file")
        print(f"   Reason: ADB command failed (return code: {users_result.returncode})")
        return False

    # 解析 JSON
    try:
        follows_data = json.loads(follows_result.stdout)
        users_data = json.loads(users_result.stdout)
    except (json.JSONDecodeError, TypeError) as e:
        print(f"❌ Failed to parse JSON data")
        print(f"   Reason: Invalid JSON format")
        print(f"   Error: {e}")
        return False

    # 检查回关
    try:
        # 获取当前用户的粉丝列表（谁关注了我）
        my_fans = [follow for follow in follows_data
                  if follow.get('followingId') == userId]

        if not my_fans:
            print(f"❌ No fans found")
            print(f"   Reason: User has no fans")
            return False

        if len(my_fans) < fanPosition:
            print(f"❌ Not enough fans")
            print(f"   Reason: User has {len(my_fans)} fan(s), expected at least {fanPosition}")
            return False

        # 获取第N个粉丝
        sorted_fans = sorted(my_fans, key=lambda x: x.get('followedAt', ''), reverse=True)
        target_fan = sorted_fans[fanPosition - 1]
        fan_id = target_fan.get('followerId')
        fan_username = target_fan.get('follower', {}).get('username', 'UNKNOWN')

        # 检查是否已经关注了这个粉丝（回关）
        has_followed_back = any(follow.get('followerId') == userId
                               and follow.get('followingId') == fan_id
                               for follow in follows_data)

        if has_followed_back:
            print(f"✓ Successfully followed back fan #{fanPosition}")
            print(f"   Fan ID: {fan_id}")
            print(f"   Fan username: {fan_username}")
            return True
        else:
            print(f"❌ Did not follow back fan #{fanPosition}")
            print(f"   Fan ID: {fan_id}")
            print(f"   Fan username: {fan_username}")
            print(f"   Reason: No follow record found from user to this fan")
            return False

    except Exception as e:
        print(f"❌ Error while checking follow back")
        print(f"   Reason: {e}")
        return False

if __name__ == "__main__":
    print(FollowBackFanCheck(
        userId='user_current',
        fanPosition=1
    ))
