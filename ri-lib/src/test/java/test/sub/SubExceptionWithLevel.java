package test.sub;

import com.github.t1.problemdetail.Logging;

import static com.github.t1.problemdetail.LogLevel.INFO;

@Logging(at = INFO)
public class SubExceptionWithLevel extends Exception {}
