package se.l4.graphql.binding.internal;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import graphql.schema.DataFetchingEnvironment;
import se.l4.graphql.binding.resolver.DataFetchingConversion;

/**
 * {@link Publisher} that applies a {@link DataFetchingConversion} to items
 * emitted.
 *
 * @param <I>
 * @param <O>
 */
public class ConvertingPublisher<I, O>
	implements Publisher<O>
{
	private final Publisher<I> source;
	private final DataFetchingConversion<I, O> conversion;
	private final DataFetchingEnvironment env;

	public ConvertingPublisher(
		Publisher<I> source,
		DataFetchingConversion<I, O> conversion,
		DataFetchingEnvironment env
	)
	{
		this.source = source;
		this.conversion = conversion;
		this.env = env;
	}

	@Override
	public void subscribe(Subscriber<? super O> subscriber)
	{
		source.subscribe(new Subscriber<I>() {
			@Override
			public void onSubscribe(Subscription subscription)
			{
				subscriber.onSubscribe(subscription);
			}

			@Override
			public void onNext(I t)
			{
				O converted = conversion.convert(env, t);
				subscriber.onNext(converted);
			}

			@Override
			public void onError(Throwable t)
			{
				subscriber.onError(t);
			}

			@Override
			public void onComplete()
			{
				subscriber.onComplete();
			}
		});
	}

}
