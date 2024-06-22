package com.crud.template;

import org.springframework.data.annotation.Id;

record EntityTemplate(@Id Long id, String name, String owner) {
}
