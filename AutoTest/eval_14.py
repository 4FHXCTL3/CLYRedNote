import subprocess
import json

def SendPrivateMessageCheck(receiverUsername, senderId, messageContent='催更'):
    """
    检查用户是否给关注的第一个博主发送了私信
    任务14: 从"我"->"关注"进入第一个关注的博主主页，点击"私信"按钮发送"催更"
    """
    # 从设备获取消息记录
    messages_result = subprocess.run(
        ['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/messages.json'],
        capture_output=True,
        encoding='utf-8',
        errors='replace'
    )

    # 从设备获取关注列表
    follows_result = subprocess.run(
        ['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/follows.json'],
        capture_output=True,
        encoding='utf-8',
        errors='replace'
    )

    # 检查命令是否成功执行
    if messages_result.returncode != 0 or not messages_result.stdout:
        print(f"❌ Failed to read messages file")
        print(f"   Reason: ADB command failed (return code: {messages_result.returncode})")
        if messages_result.stderr:
            print(f"   Error: {messages_result.stderr}")
        return False

    if follows_result.returncode != 0 or not follows_result.stdout:
        print(f"❌ Failed to read follows file")
        print(f"   Reason: ADB command failed (return code: {follows_result.returncode})")
        if follows_result.stderr:
            print(f"   Error: {follows_result.stderr}")
        return False

    # 解析 JSON
    try:
        messages_data = json.loads(messages_result.stdout)
        follows_data = json.loads(follows_result.stdout)
    except (json.JSONDecodeError, TypeError) as e:
        print(f"❌ Failed to parse JSON data")
        print(f"   Reason: Invalid JSON format")
        print(f"   Error: {e}")
        return False

    # 检查私信发送
    try:
        # 检查关注列表是否为空
        if not follows_data or len(follows_data) == 0:
            print(f"❌ Follows list is empty")
            print(f"   Reason: No follow records found")
            print(f"   Expected: At least one following relationship")
            return False

        # 获取用户的关注列表，找到第一个关注的博主
        user_follows = [f for f in follows_data if f.get('followerId') == senderId]
        if not user_follows:
            print(f"❌ No following records for user")
            print(f"   Reason: User '{senderId}' is not following anyone")
            print(f"   Expected: At least one following relationship")
            return False

        user_follows.sort(key=lambda x: x.get('followedAt', ''))
        first_following = user_follows[0].get('following', {})
        receiver_id = first_following.get('id')
        receiver_name = first_following.get('username', 'Unknown')

        # 检查消息数据是否为空
        if not messages_data or len(messages_data) == 0:
            print(f"❌ Messages list is empty")
            print(f"   Reason: No message records found")
            print(f"   Expected: Message to '{receiver_name}' (id: {receiver_id}) with content '{messageContent}'")
            return False

        # 检查是否有发送给该博主的最新消息
        for message in reversed(messages_data):
            if (message.get('sender', {}).get('id') == senderId and
                message.get('receiver', {}).get('id') == receiver_id and
                message.get('content') == messageContent and
                message.get('type') == 'TEXT'):
                sent_at = message.get('sentAt', 'Unknown')
                print(f"✓ Successfully sent private message")
                print(f"   Receiver: {receiver_name} (id: {receiver_id})")
                print(f"   Content: {messageContent}")
                print(f"   Sent at: {sent_at}")
                return True

        # Check for partial matches
        messages_to_receiver = [m for m in messages_data
                               if m.get('sender', {}).get('id') == senderId
                               and m.get('receiver', {}).get('id') == receiver_id]

        print(f"❌ Private message not found")
        print(f"   Reason: No message matching all criteria")
        print(f"   Expected receiver: {receiver_name} (id: {receiver_id})")
        print(f"   Expected content: '{messageContent}'")
        print(f"   Expected type: TEXT")

        if messages_to_receiver:
            print(f"   Found {len(messages_to_receiver)} message(s) to this receiver")
            latest = messages_to_receiver[-1]
            print(f"   Latest message content: '{latest.get('content', 'N/A')}' " +
                  f"(type: {latest.get('type', 'N/A')})")
        else:
            # Check if user sent any messages
            user_messages = [m for m in messages_data
                           if m.get('sender', {}).get('id') == senderId]
            if user_messages:
                print(f"   User sent {len(user_messages)} message(s) to other users")
            else:
                print(f"   User has not sent any messages")

        return False

    except Exception as e:
        print(f"❌ Error while checking private message")
        print(f"   Reason: {e}")
        return False

if __name__ == "__main__":
    print(SendPrivateMessageCheck(
        receiverUsername='',  # 可以留空，脚本会自动从关注列表获取
        senderId='user_current',
        messageContent='催更'
    ))
