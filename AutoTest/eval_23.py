import subprocess
import json
import sys
import io

# 设置 UTF-8 编码以支持 emoji 输出
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

def UnfollowAuthorCheck(userId, authorNickname="潮流时尚达人"):
    """
    检查用户是否取消关注了指定博主
    任务23: 在我的关注列表对"潮流时尚达人"取消关注
    """
    # 从设备获取关注列表
    follows_result = subprocess.run(
        ['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/follows.json'],
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

    # 检查取消关注
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

        # 检查是否还在关注这个博主
        still_following = any(follow.get('followerId') == userId
                             and follow.get('followingId') == author_id
                             for follow in follows_data)

        if not still_following:
            print(f"✓ Successfully unfollowed author")
            print(f"   Author: '{authorNickname}' (ID: {author_id})")
            print(f"   Status: Not following anymore")
            return True
        else:
            print(f"❌ Still following the author")
            print(f"   Author: '{authorNickname}' (ID: {author_id})")
            print(f"   Reason: Follow relationship still exists")
            # Check if was ever following
            user_follows = [f for f in follows_data if f.get('followerId') == userId]
            following_nicknames = [f.get('following', {}).get('nickname', 'UNKNOWN') for f in user_follows][:10]
            print(f"   Currently following: {following_nicknames}")
            return False

    except Exception as e:
        print(f"❌ Error while checking unfollow")
        print(f"   Reason: {e}")
        return False

if __name__ == "__main__":
    print(UnfollowAuthorCheck(
        userId='user_current',
        authorNickname='潮流时尚达人'
    ))
