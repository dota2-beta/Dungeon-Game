import { useRef, useEffect } from 'react';

/**
 * Кастомный хук для получения предыдущего значения пропса или состояния.
 * @param value Текущее значение.
 * @returns Значение из предыдущего рендера, или undefined при первом рендере.
 */
export function usePrevious<T>(value: T): T | undefined {
  const ref = useRef<T | undefined>(undefined);

  useEffect(() => {
    ref.current = value;
  }, [value]);
  return ref.current;
}