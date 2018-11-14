package network.path.mobilenode.data.http.shadowsocks

data class Profile(val host: String = "afiasvoiuasd.net",
                   val remotePort: Int = 443,
                   val password: String = "PathNetwork",
                   val method: String = "aes-256-cfb")
