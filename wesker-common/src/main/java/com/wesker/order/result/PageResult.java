package com.wesker.order.result;

import java.util.List;

public record PageResult<T>(long total, List<T> records) {
}
