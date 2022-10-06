/*
 * Copyright (c) 2022 Elastos Foundation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package elastos.carrier.node;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import elastos.carrier.kademlia.Id;
import elastos.carrier.kademlia.Node;
import elastos.carrier.kademlia.Value;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "storevalue", mixinStandardHelpOptions = true, version = "Carrier storevalue 2.0",
		description = "Store a value to the DHT.")
public class StoreValueCommand implements Callable<Integer> {
	@Option(names = { "-m", "--mutable" }, description = "Mutbale value, default is immutable value, no effect on update mode.")
	private boolean mutable = false;

	@Option(names = { "-u", "--update-value" }, description = "Existing value id to be update.")
	private String target = null;

	@Option(names = { "-r", "--recipient" }, description = "The recipient id, no effect on immutable values or update mode")
	private String recipient = null;

	@Parameters(paramLabel = "VALUE", index = "0", description = "The value text.")
	private String text = null;

	@Override
	public Integer call() throws Exception {
		Node node = Launcher.getCarrierNode();
		Value value = null;

		if (target == null) {
			if (mutable) {
				if (recipient == null) {
					value = node.createSignedValue(text.getBytes());
 				} else {
 					Id recipientId = null;
 					try {
 						recipientId = new Id(recipient);
 					} catch (Exception e) {
 						System.out.println("Invalid recipient: " + recipient);
 						return -1;
 					}

 					value = node.createEncryptedValue(recipientId, text.getBytes());
 				}
			} else {
				value = node.createValue(text.getBytes());
			}
		} else {
			Id id = null;
			try {
				id = new Id(target);
			} catch (Exception e) {
				System.out.println("Invalid value id to be update: " + target);
				return -1;
			}

			value = node.updateValue(id, text.getBytes());
		}

		CompletableFuture<Void> f = Launcher.getCarrierNode().storeValue(value);
		f.get();
		System.out.println("Value " + value.getId() + " stored.");
		return 0;
	}
}