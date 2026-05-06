package com.knowpivot.server.infrastructure.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在 Long 类型 ID 字段上，序列化时转为 String，避免 JS 精度丢失。
 * 雪花算法 ID 可达 19 位，超出 JS Number.MAX_SAFE_INTEGER (2^53-1 = 9007199254740991, 16位)。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = ToStringSerializer.class)
public @interface JsonLongId {
}
