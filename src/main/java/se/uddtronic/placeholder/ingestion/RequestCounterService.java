package se.uddtronic.placeholder.ingestion;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;

import com.vaadin.flow.shared.Registration;

@Service
public class RequestCounterService {

    private final AtomicLong counter = new AtomicLong(0);
    private final List<Consumer<Long>> listeners = new CopyOnWriteArrayList<>();

    public void increment() {
        long newCount = counter.incrementAndGet();
        notifyListeners(newCount);
    }

    public long getCount() {
        return counter.get();
    }

    public Registration register(Consumer<Long> listener) {
        listeners.add(listener);
        listener.accept(counter.get());
        return () -> listeners.remove(listener);
    }

    private void notifyListeners(long newCount) {
        listeners.forEach(listener -> listener.accept(newCount));
    }
}
