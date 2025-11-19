import subprocess
import json
import sys
import io

# 设置 UTF-8 编码以支持 emoji 输出
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

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
        print(f"❌ Failed to read follows file")
        print(f"   Reason: ADB command failed (return code: {result.returncode})")
        if result.stderr:
            print(f"   Error: {result.stderr}")
        return False

    # 解析 JSON
    try:
        data = json.loads(result.stdout)
    except (json.JSONDecodeError, TypeError) as e:
        print(f"❌ Failed to parse follows data")
        print(f"   Reason: Invalid JSON format")
        print(f"   Error: {e}")
        return False

    # 检查关注列表
    try:
        if not data or len(data) == 0:
            print(f"❌ Follows list is empty")
            print(f"   Reason: No follow records found")
            print(f"   Expected: Following '{authorUsername}'")
            return False

        # 查找用户是否关注了指定博主
        for follow in data:
            if (follow.get('followerId') == userId and
                follow.get('following', {}).get('username') == authorUsername):
                print(f"✓ Successfully followed author '{authorUsername}'")
                return True

        print(f"❌ Author not followed")
        print(f"   Reason: User '{userId}' did not follow '{authorUsername}'")
        # Show current follows
        current_follows = [f.get('following', {}).get('username', 'UNKNOWN')
                          for f in data if f.get('followerId') == userId][:5]
        if current_follows:
            print(f"   Current follows: {current_follows}")
        return False

    except Exception as e:
        print(f"❌ Error while checking follows")
        print(f"   Reason: {e}")
        return False

if __name__ == "__main__":
    print(FollowAuthorCheck(
        userId='user_current',
        authorUsername='cly_beauty'  # 小红薯美妆达人的username
    ))
