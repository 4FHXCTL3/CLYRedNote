import subprocess
import json

def LikeCommentCheck(userId):
    """
    检查用户是否对首页第二篇笔记的第一条评论进行了点赞
    任务15: 在首页进入第二篇笔记的详情页点击查看第一条评论，对评论进行"点赞"
    """
    # 从设备获取点赞记录
    result = subprocess.run(
        ['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/likes.json'],
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

    # 检查评论点赞
    try:
        if not data or len(data) == 0:
            return False

        # 查找用户对评论的点赞记录
        comment_likes = [like for like in data
                        if like.get('userId') == userId
                        and like.get('targetType') == 'COMMENT']

        # 如果有最新的评论点赞记录，返回 True
        if comment_likes:
            # 按时间排序，获取最新的点赞
            latest_like = sorted(comment_likes, key=lambda x: x.get('likedAt', ''), reverse=True)[0]
            return True

        return False

    except Exception as e:
        print(f"Error: {e}")
        return False

if __name__ == "__main__":
    print(LikeCommentCheck(
        userId='user_current'
    ))
