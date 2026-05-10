package com.timcritt.tfg.application.service;

import java.util.List;

public record BatchDeleteResult(List<Long> deleted, List<Long> notFound) {}

