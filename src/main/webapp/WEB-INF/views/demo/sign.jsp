<%--
  Created by IntelliJ IDEA.
  User: liuyandong
  Date: 2019-06-18
  Time: 10:09
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
    <script src="http://code.jquery.com/jquery-3.4.1.min.js" integrity="sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo=" crossorigin="anonymous"></script>
    <script type="text/javascript">
        function doSign() {
            var serviceName = $("#service_name").val();
            alert(serviceName)
            var accessToken = $("#access_token").val();
            var method = $("#method").val();
            var timestamp = $("#timestamp").val();
            var id = $("#id").val();
            var body = $("#body").val();
            var privateKey = $("#private_key").val();
            $.post("../demo/doSign", {serviceName: serviceName, accessToken: accessToken, method: method, timestamp: timestamp, id: id, body: body, privateKey: privateKey}, function (result) {
                var signature = result["signature"];
                $("#signature").text(result["signature"]);
                $("#url").text(result["url"]);
            }, "json");
        }
    </script>
</head>
<body>
<div style="text-align: center">
    &nbsp;&nbsp;&nbsp;serviceName:
    <select id="service_name" style="height: 30px;width: 400px;">
        <option value="appapi">appapi</option>
        <option value="posapi">posapi</option>
        <option value="webapi">webapi</option>
    </select>
    <br><br>
    access_token：<input type="text" id="access_token" style="height: 30px;width: 400px;"><br><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;method：<input id="method" type="text" style="height: 30px;width: 400px;"><br><br>
    &nbsp;&nbsp;&nbsp;timestamp：<input id="timestamp" type="text" value="${timestamp}" style="height: 30px;width: 400px;"><br><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;id：<input type="text" id="id" value="${id}" style="height: 30px;width: 400px;"><br><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;body：<textarea id="body" style="height: 100px;width: 400px;"></textarea><br><br>
    &nbsp;&nbsp;&nbsp;privateKey：<textarea id="private_key" style="height: 100px;width: 400px;">${privateKey}</textarea><br><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    <button onclick="doSign();" style="height: 40px;width: 400px;background-color: #00AAEE;border-radius: 4px;border: none;color: #FFFFFF;font-size: 16px;cursor: pointer;">生成签名</button>
</div>

<br><br><br><br>
<div style="text-align: center;">
    &nbsp;&nbsp;&nbsp;signature：<textarea id="signature" style="height: 100px;width: 400px;"></textarea>
</div>
<div style="text-align: center;">
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;url：<textarea id="url" style="height: 100px;width: 400px;"></textarea>
</div>
</body>
</html>
