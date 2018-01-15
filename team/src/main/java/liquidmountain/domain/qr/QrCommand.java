package liquidmountain.domain.qr;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;

/**
 * This class encapsulates the QR generation method that uses an external service with a HystrixCommand.
 */
public class QrCommand extends HystrixCommand<Image> {
    private static final String POOL_NAME = "Qr";
    private static final int TIMEOUT_IN_MS = 2000;
    private QrGenerator qr;

    public QrCommand(QrGenerator qrGenerator) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(POOL_NAME)).andCommandPropertiesDefaults
                (HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(TIMEOUT_IN_MS)));
        this.qr = qrGenerator;
    }

    @Override
    public Image run() {
        return qr.gen();
    }
}