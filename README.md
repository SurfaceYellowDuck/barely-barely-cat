# barely-barely-cat
This project simulates the behavior of cats on a rectangular map, taking into account their interaction at a distance.
- If two cats are at a distance not exceeding r₀, then they try to start a fight with a probability of 1
- If two cats are at a distance R₀ > r₀, they begin to hiss with a probability inversely proportional to the square of the distance between them
- If there are no rivals around the cat, he walks quietly
- Cats can stop for a while and sleep

## For start project:

```
./gradlew run
```

## Parameters

- point count. Limits: less than 100000
- refresh time. Limits: more than 25
- metric : manhattan, chebyshev, euclidean

![alt text](https://github.com/SurfaceYellowDuck/barely-barely-cat/blob/main/images/example1.gif)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
