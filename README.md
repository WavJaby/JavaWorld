# JavaWorld

A world written in Java

Players can "only" control themselves using Java code

## Player code template
```java
import com.wavjaby.javaworld.adapter.*;

public class Main extends PlayerApplication {
    @Override
    public void init(Self self) {
        console.println("Init");
    }

    @Override
    public void gameUpdate(Self self) {
        console.println("Update");
    }
}
```

### Player code example (move)

```java
import com.almasb.fxgl.core.math.Vec2;
import com.wavjaby.javaworld.adapter.*;

import java.text.DecimalFormat;

public class Main extends PlayerApplication {
    private static final DecimalFormat formatter = new DecimalFormat("0.0");
    private long time;

    @Override
    public void init(Self self) {
        time = self.getWorld().getTime();
        // Move player
        self.moveTo(new Vec2(5, 0));
    }

    @Override
    public void gameUpdate(Self self) {
        World world = self.getWorld();
        // Print every second
        if (world.getTime() - time > 1000) {
            time = world.getTime();
            // Print current position and move state
            console.println(vectorFormat(self.getPosition()) + " " + self.isMoving());
        }
    }

    private String vectorFormat(Vec2 pos) {
        return "[" + formatter.format(pos.x) + ", " + formatter.format(pos.y) + "]";
    }
}
```

#### Console out

```text
[1.0, 0.0] true
[2.0, 0.0] true
[3.0, 0.0] true
[4.0, 0.0] true
[5.0, 0.0] false
```


