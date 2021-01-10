# Album for Android

[![License](https://img.shields.io/badge/license-Apache%202-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![API](https://img.shields.io/badge/API-11%2B-green.svg?style=flat)](https://android-arsenal.com/api?level=11)

> Album is a local image selector for Android.

## Set up
Maven:
```xml
<dependency>
  <groupId>com.dsiner.lib</groupId>
  <artifactId>album</artifactId>
  <version>2.0.0</version>
</dependency>
```

or Gradle:
```groovy
// AndroidX
implementation 'com.dsiner.lib:album:2.0.0'
// Or Support
implementation 'com.dsiner.lib:album:1.0.1'
```

## Features
- Use it in Activity or Fragment
- Take a picture
- Select images
- Preview pictures in the gallery and select pictures in the gallery.
- Supports zooming, by various touch gestures.
- Supports drawing, you can draw pictures in different colors.

## Screenshot
![Artboard](https://github.com/Dsiner/Album/blob/master/image/album.png)

## How do I use it?
Preview:
```java
    PhotoPreviewActivity.openActivity(this, Arrays.asList(url1, url2, url3), 1);
```

Take a picture:
```java
    CaptureActivity.openActivityForResult(this, REQUEST_CODE_CAPTURE);
```

Select images:
```java
    Album.with(this)
            .capture(false)
            .spanCount(4)
            .originEnable(true)
            .maxSelectable(9)
            .startActivityForResult(REQUEST_CODE_ALBUM);
```

## Thanks
- [PhotoView](https://github.com/chrisbanes/PhotoView)
- [Matisse](https://github.com/zhihu/Matisse)

## Licence

```txt
Copyright 2017 D

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
