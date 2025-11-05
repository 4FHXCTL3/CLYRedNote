import subprocess
import json
import sys
import io

# 设置 UTF-8 编码以支持 emoji 输出
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

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
        print(f"❌ Failed to read shares file")
        print(f"   Reason: ADB command failed (return code: {result.returncode})")
        if result.stderr:
            print(f"   Error: {result.stderr}")
        return False

    # 解析 JSON
    try:
        data = json.loads(result.stdout)
    except (json.JSONDecodeError, TypeError) as e:
        print(f"❌ Failed to parse shares data")
        print(f"   Reason: Invalid JSON format")
        print(f"   Error: {e}")
        return False

    # 检查分享记录
    try:
        if not data or len(data) == 0:
            print(f"❌ Shares list is empty")
            print(f"   Reason: No share records found")
            print(f"   Expected: At least one share record for user '{userId}'")
            return False

        # 查找用户的分享记录
        user_shares = [item for item in data if item.get('userId') == userId]

        # 如果有最新的分享记录，返回 True
        if user_shares:
            # 按时间排序，获取最新的分享
            latest_share = sorted(user_shares, key=lambda x: x.get('sharedAt', ''), reverse=True)[0]
            shared_note_id = latest_share.get('noteId', 'Unknown')
            shared_at = latest_share.get('sharedAt', 'Unknown')
            print(f"✓ Successfully shared a note")
            print(f"   Note ID: {shared_note_id}")
            print(f"   Shared at: {shared_at}")
            return True

        print(f"❌ No share records for user")
        print(f"   Reason: User '{userId}' has not shared any notes")
        print(f"   Expected: At least one share record")
        print(f"   Total share records in system: {len(data)}")
        return False

    except Exception as e:
        print(f"❌ Error while checking share records")
        print(f"   Reason: {e}")
        return False

if __name__ == "__main__":
    print(ShareNoteCheck(
        userId='user_current'
    ))
