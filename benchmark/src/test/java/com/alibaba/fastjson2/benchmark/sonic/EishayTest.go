package main

import (
	"fmt"
	"github.com/bytedance/sonic"
	"time"
)

const (
	SMALL = "SMALL"
	LARGE = "LARGE"
)

const (
	JAVA  = "JAVA"
	FLASH = "FLASH"
)

var str = "{\"images\": [{\n      \"height\":768,\n      \"size\":\"LARGE\",\n      \"title\":\"Javaone Keynote\",\n      \"uri\":\"http://javaone.com/keynote_large.jpg\",\n      \"width\":1024\n    }, {\n      \"height\":240,\n      \"size\":\"SMALL\",\n      \"title\":\"Javaone Keynote\",\n      \"uri\":\"http://javaone.com/keynote_small.jpg\",\n      \"width\":320\n    }\n  ],\n  \"media\": {\n    \"bitrate\":262144,\n    \"duration\":18000000,\n    \"format\":\"video/mpg4\",\n    \"height\":480,\n    \"persons\": [\n      \"Bill Gates\",\n      \"Steve Jobs\"\n    ],\n    \"player\":\"JAVA\",\n    \"size\":58982400,\n    \"title\":\"Javaone Keynote\",\n    \"uri\":\"http://javaone.com/keynote.mpg\",\n    \"width\":640\n  }\n}"

type Media struct {
	Bitrate   int32     `json:"bitrate"`
	Duration  int64     `json:"duration"`
	Format    string    `json:"format"`
	Height    int32     `json:"height"`
	Persons   []*string `json:"persons"`
	Player    string    `json:"player"`
	Size      int64     `json:"size"`
	Title     string    `json:"title"`
	Uri       string    `json:"uri"`
	Width     int32     `json:"width"`
	Copyright string    `json:"copyright"`
}

type Image struct {
	Height int32  `json:"height"`
	Size   string `json:"size"`
	Title  string `json:"title"`
	Uri    string `json:"uri"`
	Width  int32  `json:"width"`
}

type MediaContent struct {
	Media  *Media   `json:"media""`
	Images []*Image `json:"images""`
}

func main() {
	mediaContent := &MediaContent{}
	err := sonic.UnmarshalString(str, mediaContent)
	if err != nil {
		fmt.Printf("sonic.UnmarshalString error: %v\n", err)
	}

	//WARM_LOOP_COUNT := 1000
	//{
	//	for i := 0; i < WARM_LOOP_COUNT; i++ {
	//		sonic.MarshalString(mediaContent)
	//	}
	//}
	//
	//{
	//	tmp := &MediaContent{}
	//	for i := 0; i < WARM_LOOP_COUNT; i++ {
	//		sonic.UnmarshalString(str, tmp)
	//	}
	//}

	LOOP_COUNT := 1000000
	for j := 0; j < 5; j++ {
		start := time.Now()
		for i := 0; i < LOOP_COUNT; i++ {
			sonic.MarshalString(mediaContent)
		}
		fmt.Printf("sonic eishay MarshalString(toJSONString) time: %v \n", time.Now().Sub(start).Milliseconds())
	}

	for j := 0; j < 5; j++ {
		tmp := &MediaContent{}
		start := time.Now()
		for i := 0; i < LOOP_COUNT; i++ {
			sonic.UnmarshalString(str, tmp)
		}
		fmt.Printf("sonic eishay UnmarshalString(parseObject) time: %v \n", time.Now().Sub(start).Milliseconds())
	}

}
