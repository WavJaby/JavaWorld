# JavaWorldSDK

- Player example code. [ExampleCode](../../../../../../../PlayerExample/src/main/java/com)

### Player code template

```java
import com.javaworld.adapter.*;
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

#### Console out

```text
Init
Update
Update
Update
...
```

### Player code example (move)

```java
import com.javaworld.adapter.*;
import com.almasb.fxgl.core.math.*;
import java.text.*;
import java.util.*;

public class Main extends PlayerApplication {
    @Override
    public void init(Self self) {
        console.println("Init");
        self.moveTo(new Vec2(0, 10));
    }

    @Override
    public void gameUpdate(Self self) {
        console.println(self.getPosition());
    }
}
```
