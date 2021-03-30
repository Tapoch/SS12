package ru.alex.ss12.response

import ru.alex.ss12.game.model.Item

class ItemsResponse(val items: Collection<Item>) : Response(Type.ITEMS)
