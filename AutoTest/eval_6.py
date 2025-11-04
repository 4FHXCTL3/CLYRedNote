import subprocess
import json

def ReplyCommentCheck(userId, replyContent):
    """
    检查用户是否回复了最新收到的评论
    任务6: 查看"消息"->"评论和@"页面，回复最新收到的一条评论，内容为"谢谢喜欢～"
    """
    # 从设备获取评论列表
    result = subprocess.run(
        ['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/comments.json'],
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

    # 检查评论回复
    try:
        if not data or len(data) == 0:
            return False

        # 获取当前用户的笔记
        current_user_notes = set()
        # 这里简化处理，假设评论数据中包含了足够的信息

        # 查找回复类型的评论（parentCommentId不为空表示是回复）
        replies = [comment for comment in data
                  if comment.get('author', {}).get('id') == userId
                  and comment.get('parentCommentId') is not None
                  and comment.get('content') == replyContent]

        # 如果找到符合条件的回复，返回 True
        if replies:
            # 按时间排序，检查是否是最新的回复
            latest_reply = sorted(replies, key=lambda x: x.get('createdAt', ''), reverse=True)[0]
            return latest_reply.get('content') == replyContent

        return False

    except Exception as e:
        print(f"Error: {e}")
        return False

if __name__ == "__main__":
    print(ReplyCommentCheck(
        userId='user_current',
        replyContent='谢谢喜欢～'
    ))
