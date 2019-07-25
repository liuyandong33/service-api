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
    <script type="text/javascript" src="https://code.jquery.com/jquery-3.4.1.min.js"></script>
    <script type="text/javascript">
        function doSign() {
            var serviceName = $("#service_name").val();
            var apiVersion = $("#api_version").val();
            var accessToken = $("#access_token").val();
            var method = $("#method").val();
            var timestamp = $("#timestamp").val();
            var id = $("#id").val();
            var body = $("#body").val();
            var privateKey = $("#private_key").val();

            var data = undefined;
            $.ajax({
                type: "POST",
                cache: false,
                async: false,
                url: "../demo/doSign",
                dataType: "json",
                data: {serviceName: serviceName, apiVersion: apiVersion, accessToken: accessToken, method: method, timestamp: timestamp, id: id, body: body, privateKey: privateKey},
                success: function (result) {
                    data = result;
                }
            });
            return data;
        }

        function sign() {
            debugger
            var result = doSign();
            $("#signature").val(result["signature"]);
            $("#url").val(result["url"]);
        }

        function doCall() {
            var info = doSign();

            $("#signature").val(info["signature"]);
            $("#url").val(info["url"]);

            $.ajax({
                type: "POST",
                cache: false,
                async: false,
                url: info["url"],
                contentType: "application/json",
                dataType: "json",
                data: $("#body").val(),
                success: function (result) {
                    $("#result").val(JSON.stringify(result));
                }
            });
        }
    </script>
    <style type="text/css">
        .item_name {
            width: 150px;
            display: inline-block;
            white-space: nowrap;
            text-align: right;
            margin-right: 10px;
        }
    </style>
</head>
<body>
<div style="text-align: center">
    <span class="item_name">serviceName:</span>
    <select id="service_name" style="height: 30px;width: 400px;">
        <option value="appapi">appapi</option>
        <option value="posapi">posapi</option>
        <option value="webapi">webapi</option>
    </select>
    <br><br>

    <span class="item_name">apiVersion:</span>
    <select id="api_version" style="height: 30px;width: 400px;">
        <option value="v1">v1</option>
    </select>
    <br><br>

    <span class="item_name">access_token:</span>
    <input type="text" id="access_token" style="height: 30px;width: 400px;">
    <br><br>

    <span class="item_name">method:</span>
    <input id="method" type="text" style="height: 30px;width: 400px;">
    <br><br>

    <span class="item_name">timestamp:</span>
    <input id="timestamp" type="text" value="${timestamp}" style="height: 30px;width: 400px;">
    <br><br>

    <span class="item_name">id:</span>
    <input type="text" id="id" value="${id}" style="height: 30px;width: 400px;">
    <br><br>

    <span class="item_name">body:</span>
    <textarea id="body" style="height: 100px;width: 400px;"></textarea>
    <br><br>

    <span class="item_name">privateKey:</span>
    <textarea id="private_key" style="height: 100px;width: 400px;">${privateKey}</textarea>
    <br><br>

    <span class="item_name"></span>
    <button onclick="sign();" style="height: 40px;width: 400px;background-color: #00AAEE;border-radius: 4px;border: none;color: #FFFFFF;font-size: 16px;cursor: pointer;">生成签名</button>
    <br><br>

    <span class="item_name"></span>
    <button onclick="doCall();" style="height: 40px;width: 400px;background-color: #00AAEE;border-radius: 4px;border: none;color: #FFFFFF;font-size: 16px;cursor: pointer;">调用</button>
</div>

<br><br><br><br>
<div style="text-align: center;">
    <span class="item_name">signature:</span>
    <textarea id="signature" style="height: 100px;width: 400px;"></textarea>
</div>
<div style="text-align: center;">
    <span class="item_name">url:</span>
    <textarea id="url" style="height: 100px;width: 400px;"></textarea>
</div>
<div style="text-align: center;">
    <span class="item_name">结果:</span>
    <textarea id="result" style="height: 100px;width: 400px;"></textarea>
</div>
</body>
</html>
