package se.l4.graphql.binding.internal.schema;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import graphql.ExecutionResult;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import se.l4.graphql.binding.GraphQLBinder;
import se.l4.graphql.binding.annotations.GraphQLField;
import se.l4.graphql.binding.annotations.GraphQLSubscription;
import se.l4.graphql.binding.internal.GraphQLTest;

public class SubscriptionTest
	extends GraphQLTest
{

	@Override
	protected void setup(GraphQLBinder binder)
	{
		binder.withRoot(new Root());
	}

	@Test
	public void testExecute()
	{
		Result result = execute("subscription S { reactive }");
		result.assertNoErrors();

		assertThat(result.getData(), is(instanceOf(Publisher.class)));

		Publisher<ExecutionResult> publisher = result.getData();

		AtomicInteger count = new AtomicInteger();
		publisher.subscribe(new Subscriber<ExecutionResult>()
		{
			private int counter;

			@Override
			public void onSubscribe(Subscription s)
			{
				s.request(10);
			}

			@Override
			public void onNext(ExecutionResult data)
			{
				counter++;
			}

			@Override
			public void onError(Throwable t)
			{
				fail();
			}

			@Override
			public void onComplete()
			{
				count.set(counter);
			}
		});
		assertThat(count.get(), is(2));
	}

	public class Root
		extends Parent
	{
		@GraphQLField
		public String none()
		{
			return null;
		}
	}

	public class Parent
	{
		@GraphQLSubscription
		public Publisher<Integer> reactive()
		{
			Observable<Integer> observable = Observable.create(emitter -> {
				for(int i=0; i<2; i++)
				{
					emitter.onNext(1);
					Thread.sleep(500);
				}
                emitter.onComplete();
            });

            return observable.toFlowable(BackpressureStrategy.BUFFER);
		}
	}
}
