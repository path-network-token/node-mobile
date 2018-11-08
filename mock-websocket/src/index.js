const WebSocket = require('ws');

const server = new WebSocket.Server({port: 8080});
const stdin = process.openStdin();

server.on('connection', function connection(connection) {

    var inputListener = function (input) {
        connection.send(input.toString());
    }

    var pinger = setInterval(function () {
        connection.send('{"type":"ack","node_id":"1","id":"1"}');
    }, 25000);

    stdin.addListener("data", inputListener);

    connection.on('message', function incoming(message) {
        console.log('Received: %s', message);
    });

    connection.on('close', function(reasonCode, description) {
        stdin.removeListener("data", inputListener);
        clearInterval(pinger);
        console.log('Peer ' + connection.remoteAddress + ' disconnected.');
    });

});
