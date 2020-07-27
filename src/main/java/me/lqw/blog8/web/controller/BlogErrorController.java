package me.lqw.blog8.web.controller;

import me.lqw.blog8.util.JacksonUtil;
import me.lqw.blog8.web.controller.console.BaseController;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Map;

import static me.lqw.blog8.exception.resolver.BlogHandlerExceptionResolver.ERROR_ATTRIBUTES;

/**
 * 系统出错处理
 * @author liqiwen
 * @version 1.2
 * @since 1.2
 */
@Controller
@RequestMapping("error")
public class BlogErrorController extends BaseController implements ErrorController {

    /**
     * 错误属性，可以从该属性中获取请求的错误信息
     * @see me.lqw.blog8.exception.resolver.BlogHandlerExceptionResolver
     * 在上面的这个类中，我们移除了系统自带的错误，填充了我们自定义的错误
     */
    private final ErrorAttributes errorAttributes;

    public BlogErrorController(ErrorAttributes errorAttributes) {
        super();
        this.errorAttributes = errorAttributes;
    }

    /**
     * 获取系统默认的错误路径
     * @return string
     */
    @Override
    public String getErrorPath() {
        return "/error";
    }

    /**
     * 给前端返回 html 格式的错误
     * @param request request
     * @param response response
     * @return ModelAndView
     */
    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response){
        logger.error("BlogErrorController#error....");
        ModelAndView mav = getModelAndView();
        HttpStatus status = getStatus(request);

        Map<String, Object> errors = errorAttributes.getErrorAttributes(new ServletWebRequest(request),
                ErrorAttributeOptions.of(Collections.emptyList()));

        response.setStatus(status.value());
        mav.setViewName("page_error");
        mav.addObject("errors", errors);
        return mav;
    }

    /**
     * 给前端返回 json 格式的数据
     * @param request request
     * @param response response
     * @return ResponseEntity
     */
    @RequestMapping
    @ResponseBody
    public ResponseEntity<Map<String, Object>> errorJSON(HttpServletRequest request, HttpServletResponse response){
        logger.error("BlogErrorController#errorJSON()...");
        HttpStatus status = getStatus(request);
        if(!status.isError()){
            //非 4xx 或者 5xx 的错误
            return new ResponseEntity<>(status);
        }
        Map<String, Object> errors = errorAttributes.getErrorAttributes(new ServletWebRequest(request),
                ErrorAttributeOptions.of(Collections.emptyList()));
        logger.error("BlogErrorController#errorJSON() return:[{}]", JacksonUtil.toJsonString(errors));
        return new ResponseEntity<>(errors, status);
    }

    /**
     * 从 httpServletRequest 中获取本次 http 请求的状态码
     * @param request request
     * @return HttpStatus
     */
    public HttpStatus getStatus(HttpServletRequest request){
        Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (statusCode == null) {
            if(request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI) != null) {
                return HttpStatus.INTERNAL_SERVER_ERROR;
            } else {
                return HttpStatus.OK;
            }
        }
        try {
            return HttpStatus.valueOf(statusCode);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
