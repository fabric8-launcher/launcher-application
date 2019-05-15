import React from "react";
import { render, fireEvent } from "react-testing-library";
import { DependenciesPicker } from "../dependencies-picker";

describe('<QuarkusDependencyPicker />', () => {

  const items = [
    {
      "id": "io.quarkus:quarkus-arc",
      "description": "Build time CDI dependency injection",
      "name": "ArC",
      "metadata": { "category": "Core" }
    },
    {
      "name": "Netty",
      "description": "Netty is a non-blocking I/O client-server framework. Used by Quarkus as foundation layer.",
      "id": "io.quarkus:quarkus-netty",
      "metadata": { "category": "Web" }
    },
  ]

  it('renders the QuarkusDependencyPicker correctly', () => {
    const comp = render(<DependenciesPicker.Element placeholder="" items={items} value={{}} onChange={() => { }} />);
    expect(comp.asFragment()).toMatchSnapshot();
  });

  it('show results for valid search', async () => {
    const comp = render(<DependenciesPicker.Element placeholder="" items={items} value={{}} onChange={() => {}} />);

    const searchField = comp.getByLabelText('Search dependencies');
    fireEvent.change(searchField, { target: { value: 'CDI' } });
    const result = await comp.findAllByText(items[0].description);
    expect((result as HTMLElement[]).length).toBe(1);
    expect(comp.asFragment()).toMatchSnapshot();
  });

  it('select values and save', async () => {
    const handleChange = jest.fn();
    const comp = render(<DependenciesPicker.Element placeholder="" items={items} value={{}} onChange={handleChange} />);

    const searchField = comp.getByLabelText('Search dependencies');
    fireEvent.change(searchField, { target: { value: 'Netty' } });
    const item = await comp.findAllByText(items[1].description);
    fireEvent.click(item[0]);
    expect(comp.asFragment()).toMatchSnapshot();
  });

});
