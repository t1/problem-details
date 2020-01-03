package test;

import com.github.t1.problemdetail.Detail;
import com.github.t1.problemdetail.Extension;
import com.github.t1.problemdetaildemoapp.DemoService.DemoException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@ResponseStatus(value = NOT_FOUND, reason = "article not found")
@AllArgsConstructor @NoArgsConstructor
public class ArticleNotFoundException extends DemoException {
    @Extension @Getter String article;

    @Detail String getDetail() { return "There is no article [" + article + "]"; }
}
