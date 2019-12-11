package test.sub;

import com.github.t1.problemdetail.Logging;

@Logging(to = "sub-cat")
public class SubExceptionWithCategory extends Exception {}
