该参数控制生物将会属于什么生成类型。

* 标记为“怪物”（Monster）的生物只会在黑暗处或晚上生成。
* 标记为“生物”（Creature）的生物只会在阳光直射下的草质方块上生成。不要对怪物使用这个类型，这样会阻止它们生成。
* 标记为“环境”（Ambient）的生物会在任何条件下生成，除非方块类型不允许。但是这个类型应当仅用于没什么游戏影响的生物，比如蝙蝠。
* 标记为“水生生物”（WaterCreature）的生物将会在水中生成，但是没有其它限制。

生成类型系统在 [此处](https://mcreator.net/wiki/mob-spawning-parameters) 有着深入的介绍。