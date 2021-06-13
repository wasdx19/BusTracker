package com.example.busstopreminder.service

import com.example.busstopreminder.data.point.PointDTO
import com.example.busstopreminder.data.route.RouteItemDTO
import com.example.busstopreminder.data.route.RoutesDTO

class RoutesDelegate() {

    fun getRoute() = RoutesDTO(
        routeItems = mutableListOf<RouteItemDTO>().apply {
            getRouteItem()
        }
    )

    private fun getRouteItem() = RouteItemDTO(
        id = 0,
        color = 0,
        routeName = "30",
        busCount = 10,
        polylinePoints = arrayListOf<PointDTO>(
            PointDTO(
                43.24275803,
                76.84589273
            ),
            PointDTO(
                43.24296514,
                76.84685296
            ),
            PointDTO(
                43.24328797,
                76.84837818
            ),
            PointDTO(
                43.2436553,
                76.84954762
            ),
            PointDTO(
                43.24444464,
                76.85140371
            ),
            PointDTO(
                43.24506205,
                76.8533349
            ),
            PointDTO(
                43.24545281,
                76.85515881
            ),
            PointDTO(
                43.24563256,
                76.85590982
            ),
            PointDTO(
                43.24503079,
                76.85630679
            ),
            PointDTO(
                43.24346773,
                76.85711145
            ),
            PointDTO(
                43.24193589,
                76.85786247
            ),
            PointDTO(
                43.24038133,
                76.85863076
            ),
            PointDTO(
                43.23982641,
                76.85890434
            ),
            PointDTO(
                43.23979435,
                76.85894883
            ),
            PointDTO(
                43.23959357,
                76.85907769
            ),
            PointDTO(
                43.23834351,
                76.85963168
            ),
            PointDTO(
                43.23754235,
                76.86005011
            ),
            PointDTO(
                43.23654909,
                76.86060193
            ),
            PointDTO(
                43.2355486,
                76.86272624
            ),
            PointDTO(
                43.23475132,
                76.86442139
            ),
            PointDTO(
                43.23376021,
                76.86635971
            ),
            PointDTO(
                43.23300981,
                76.86744332
            ),
            PointDTO(
                43.23198999,
                76.86893227
            ),
            PointDTO(
                43.23295146,
                76.87074545
            ),
            PointDTO(
                43.23364687,
                76.87165439
            ),
            PointDTO(
                43.23455939,
                76.87288542
            ),
            PointDTO(
                43.23530823,
                76.87416485
            ),
            PointDTO(
                43.23583754,
                76.87522727
            ),
            PointDTO(
                43.23598214,
                76.87557059
            ),
            PointDTO(
                43.23641782,
                76.87652528
            ),
            PointDTO(
                43.236793,
                76.87768936
            ),
            PointDTO(
                43.23709002,
                76.87859058
            ),
            PointDTO(
                43.23736645,
                76.87972025
            )
        )
    )
}