Dev Info
--------

Test Browser Run
```
./gradlew -t wasmJsBrowserTestRun
```

Run Tests
```
./gradlew -t wasmJsBrowserTest
```

Release
```
git tag v0.0.x
git push --atomic origin main v0.0.x
```