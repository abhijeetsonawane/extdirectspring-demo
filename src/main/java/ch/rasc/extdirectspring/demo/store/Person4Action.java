/**
 * Copyright 2010-2014 Ralph Schaer <ralphschaer@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.rasc.extdirectspring.demo.store;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.ralscha.extdirectspring.annotation.ExtDirectMethod;
import ch.ralscha.extdirectspring.annotation.ExtDirectMethodType;
import ch.ralscha.extdirectspring.bean.ExtDirectStoreReadRequest;
import ch.ralscha.extdirectspring.bean.ExtDirectStoreResult;
import ch.rasc.extdirectspring.demo.util.PropertyComparatorFactory;

@Service
public class Person4Action {

	@Autowired
	private RandomDataBean dataBean;

	@ExtDirectMethod(value = ExtDirectMethodType.STORE_READ, group = "store4")
	public List<Person> load(ExtDirectStoreReadRequest request) {
		List<Person> persons = dataBean.findPersons(request.getQuery());
		return persons.subList(0, Math.min(50, persons.size()));
	}

	@ExtDirectMethod(value = ExtDirectMethodType.STORE_READ, group = "store4")
	public ExtDirectStoreResult<Person> loadWithPaging(ExtDirectStoreReadRequest request) {

		List<Person> persons = dataBean.findPersons(request.getQuery());
		int totalSize = persons.size();

		Stream<Person> personsStream = persons.stream();
		Comparator<Person> comparator = PropertyComparatorFactory.createComparatorFromSorters(request.getSorters());
		if (comparator != null) {
			personsStream = personsStream.sorted(comparator);
		}

		if (request.getStart() != null && request.getLimit() != null) {
			personsStream = personsStream.skip(request.getStart()).limit(request.getLimit());
		}

		return new ExtDirectStoreResult<>(totalSize, personsStream.collect(Collectors.toList()));
	}

	@ExtDirectMethod(value = ExtDirectMethodType.STORE_MODIFY, group = "store4")
	public List<Person> create(List<Person> newPersons) {
		return newPersons.stream().map(p -> dataBean.insert(p)).collect(Collectors.toList());
	}

	@ExtDirectMethod(value = ExtDirectMethodType.STORE_MODIFY, group = "store4")
	public List<Person> update(List<Person> modifiedPersons) {

		return modifiedPersons.stream().map(person -> {
			Person p = dataBean.findPerson(person.getId());
			if (p != null) {
				p.update(person);
				return p;
			}
			return null;
		}).filter(Objects::nonNull).collect(Collectors.toList());

	}

	@ExtDirectMethod(value = ExtDirectMethodType.STORE_MODIFY, group = "store4")
	public void destroy(List<Person> destroyPersons) {
		for (Person person : destroyPersons) {
			dataBean.deletePerson(person);
		}
	}

	@ExtDirectMethod(value = ExtDirectMethodType.STORE_READ, group = "store4")
	public Set<State> getStates() {
		return dataBean.findPersons(null).stream().map(Person::getState).map(State::new).collect(Collectors.toSet());
	}

}
